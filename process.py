import numpy as np
import xarray as xr
import re
from pathlib import Path
import collections
import seaborn as sns
import pandas as pd
sns.set_theme(style="darkgrid")
sns.set_context("paper")
sns.set(font_scale=1.8)
def distance(val, ref):
    return abs(ref - val)
vectDistance = np.vectorize(distance)

def cmap_xmap(function, cmap):
    """ Applies function, on the indices of colormap cmap. Beware, function
    should map the [0, 1] segment to itself, or you are in for surprises.

    See also cmap_xmap.
    """
    cdict = cmap._segmentdata
    function_to_map = lambda x : (function(x[0]), x[1], x[2])
    for key in ('red','green','blue'):
        cdict[key] = map(function_to_map, cdict[key])
#        cdict[key].sort()
#        assert (cdict[key][0]<0 or cdict[key][-1]>1), "Resulting indices extend out of the [0, 1] segment."
    return matplotlib.colors.LinearSegmentedColormap('colormap',cdict,1024)

def getClosest(sortedMatrix, column, val):
    while len(sortedMatrix) > 3:
        half = int(len(sortedMatrix) / 2)
        sortedMatrix = sortedMatrix[-half - 1:] if sortedMatrix[half, column] < val else sortedMatrix[: half + 1]
    if len(sortedMatrix) == 1:
        result = sortedMatrix[0].copy()
        result[column] = val
        return result
    else:
        safecopy = sortedMatrix.copy()
        safecopy[:, column] = vectDistance(safecopy[:, column], val)
        minidx = np.argmin(safecopy[:, column])
        safecopy = safecopy[minidx, :].A1
        safecopy[column] = val
        return safecopy

def convert(column, samples, matrix):
    return np.matrix([getClosest(matrix, column, t) for t in samples])

def valueOrEmptySet(k, d):
    return (d[k] if isinstance(d[k], set) else {d[k]}) if k in d else set()

def mergeDicts(d1, d2):
    """
    Creates a new dictionary whose keys are the union of the keys of two
    dictionaries, and whose values are the union of values.

    Parameters
    ----------
    d1: dict
        dictionary whose values are sets
    d2: dict
    parameters: 50.0
        dictionary whose values are sets

    Returns
    -------
    dict
        A dict whose keys are the union of the keys of two dictionaries,
    and whose values are the union of values

    """
    res = {}
    for k in d1.keys() | d2.keys():
        res[k] = valueOrEmptySet(k, d1) | valueOrEmptySet(k, d2)
    return res

def extractCoordinates(filename):
    """
    Scans the header of an Alchemist file in search of the variables.

    Parameters
    ----------
    filename : str
        path to the target file
    mergewith : dict
        a dictionary whose dimensions will be merged with the returned one

    Returns
    -------
    dict
        A dictionary whose keys are strings (coordinate name) and values are
        lists (set of variable values)

    """
    with open(filename, 'r') as file:
#        regex = re.compile(' (?P<varName>[a-zA-Z._-]+) = (?P<varValue>[-+]?\d*\.?\d+(?:[eE][-+]?\d+)?),?')
        regex = r"(?P<varName>[a-zA-Z._-]+) = (?P<varValue>[^,]*),?"
        dataBegin = r"\d"
        is_float = r"[-+]?\d*\.?\d+(?:[eE][-+]?\d+)?"
        for line in file:
            match = re.findall(regex, line.replace('Infinity', '1e30000'))
            if match:
                return {
                    var : float(value) if re.match(is_float, value)
                        else bool(re.match(r".*?true.*?", value.lower())) if re.match(r".*?(true|false).*?", value.lower())
                        else value
                    for var, value in match
                }
            elif re.match(dataBegin, line[0]):
                return {}

def extractVariableNames(filename):
    """
    Gets the variable names from the Alchemist data files header.

    Parameters
    ----------
    filename : str
        path to the target file

    Returns
    -------
    list of list
        A matrix with the values of the csv file

    """
    with open(filename, 'r') as file:
        dataBegin = re.compile('\d')
        lastHeaderLine = ''
        for line in file:
            if dataBegin.match(line[0]):
                break
            else:
                lastHeaderLine = line
        if lastHeaderLine:
            regex = re.compile(' (?P<varName>\S+)')
            return regex.findall(lastHeaderLine)
        return []

def openCsv(path):
    """
    Converts an Alchemist export file into a list of lists representing the matrix of values.

    Parameters
    ----------
    path : str
        path to the target file

    Returns
    -------
    list of list
        A matrix with the values of the csv file

    """
    regex = re.compile('\d')
    with open(path, 'r') as file:
        lines = filter(lambda x: regex.match(x[0]), file.readlines())
        return [[float(x) for x in line.split()] for line in lines]

def beautifyValue(v):
    """
    Converts an object to a better version for printing, in particular:
        - if the object converts to float, then its float value is used
        - if the object can be rounded to int, then the int value is preferred

    Parameters
    ----------
    v : object
        the object to try to beautify

    Returns
    -------
    object or float or int
        the beautified value
    """
    try:
        v = float(v)
        if v.is_integer():
            return int(v)
        return v
    except:
        return v

if __name__ == '__main__':
    # CONFIGURE SCRIPT
    # Where to find Alchemist data files
    directory = 'data'
    # Where to save charts
    output_directory = 'charts'
    # How to name the summary of the processed data
    pickleOutput = 'data_summary'
    # Experiment prefixes: one per experiment (root of the file name)
    experiments = ['gradient', 'channel']
    floatPrecision = '{: 0.3f}'
    # Number of time samples 
    timeSamples = 200
    # time management
    minTime = 0
    maxTime = 300
    timeColumnName = 'time'
    logarithmicTime = False
    # One or more variables are considered random and "flattened"
    seedVars = ['seed', 'longseed', "spacing", "error"]
    # Label mapping
    class Measure:
        def __init__(self, description, unit = None):
            self.__description = description
            self.__unit = unit
        def description(self):
            return self.__description
        def unit(self):
            return '' if self.__unit is None else f'({self.__unit})'
        def derivative(self, new_description = None, new_unit = None):
            def cleanMathMode(s):
                return s[1:-1] if s[0] == '$' and s[-1] == '$' else s
            def deriveString(s):
                return r'$d ' + cleanMathMode(s) + r'/{dt}$'
            def deriveUnit(s):
                return f'${cleanMathMode(s)}' + '/{s}$' if s else None
            result = Measure(
                new_description if new_description else deriveString(self.__description),
                new_unit if new_unit else deriveUnit(self.__unit),
            )
            return result
        def __str__(self):
            return f'{self.description()} {self.unit()}'
    
    centrality_label = 'H_a(x)'
    def expected(x):
        return r'\mathbf{E}[' + x + ']'
    def stdev_of(x):
        return r'\sigma{}[' + x + ']'
    def mse(x):
        return 'MSE[' + x + ']'
    def cardinality(x):
        return r'\|' + x + r'\|'

    labels = {
        'root[mean]': Measure(r'output'),
        'ticks[sum]': Measure(r'ticks', 'count'),
        'message[sum]': Measure("messages", "count")
    }
    def derivativeOrMeasure(variable_name):
        if variable_name.endswith('dt'):
            return labels.get(variable_name[:-2], Measure(variable_name)).derivative()
        return Measure(variable_name)
    def label_for(variable_name):
        return labels.get(variable_name, derivativeOrMeasure(variable_name)).description()
    def unit_for(variable_name):
        return str(labels.get(variable_name, derivativeOrMeasure(variable_name)))
    
    # Setup libraries
    np.set_printoptions(formatter={'float': floatPrecision.format})
    # Read the last time the data was processed, reprocess only if new data exists, otherwise just load
    import pickle
    import os
    if os.path.exists(directory):
        newestFileTime = max([os.path.getmtime(directory + '/' + file) for file in os.listdir(directory)], default=0.0)
        try:
            lastTimeProcessed = pickle.load(open('timeprocessed', 'rb'))
        except:
            lastTimeProcessed = -1
        shouldRecompute = True
        if not shouldRecompute:
            try:
                means = pickle.load(open(pickleOutput + '_mean', 'rb'))
                stdevs = pickle.load(open(pickleOutput + '_std', 'rb'))
            except:
                shouldRecompute = True
        if shouldRecompute:
            timefun = np.logspace if logarithmicTime else np.linspace
            means = {}
            stdevs = {}
            for experiment in experiments:
                # Collect all files for the experiment of interest
                import fnmatch
                allfiles = filter(lambda file: fnmatch.fnmatch(file, experiment + '_*.csv'), os.listdir(directory))
                allfiles = [directory + '/' + name for name in allfiles]
                allfiles.sort()
                # From the file name, extract the independent variables
                dimensions = {}
                for file in allfiles:
                    dimensions = mergeDicts(dimensions, extractCoordinates(file))
                dimensions = {k: sorted(v) for k, v in dimensions.items()}
                # Add time to the independent variables
                dimensions[timeColumnName] = range(0, timeSamples)
                # Compute the matrix shape
                shape = tuple(len(v) for k, v in dimensions.items())
                # Prepare the Dataset
                dataset = xr.Dataset()
                for k, v in dimensions.items():
                    dataset.coords[k] = v
                if len(allfiles) == 0:
                    print("WARNING: No data for experiment " + experiment)
                    means[experiment] = dataset
                    stdevs[experiment] = xr.Dataset()
                else:
                    varNames = extractVariableNames(allfiles[0])
                    for v in varNames:
                        if v != timeColumnName:
                            novals = np.ndarray(shape)
                            novals.fill(float('nan'))
                            dataset[v] = (dimensions.keys(), novals)
                    # Compute maximum and minimum time, create the resample
                    timeColumn = varNames.index(timeColumnName)
                    allData = { file: np.matrix(openCsv(file)) for file in allfiles }
                    computeMin = minTime is None
                    computeMax = maxTime is None
                    if computeMax:
                        maxTime = float('-inf')
                        for data in allData.values():
                            maxTime = max(maxTime, data[-1, timeColumn])
                    if computeMin:
                        minTime = float('inf')
                        for data in allData.values():
                            minTime = min(minTime, data[0, timeColumn])
                    timeline = timefun(minTime, maxTime, timeSamples)
                    # Resample
                    for file in allData:
                        print(file)
                        allData[file] = convert(timeColumn, timeline, allData[file])
                    # Populate the dataset
                    for file, data in allData.items():
                        dataset[timeColumnName] = timeline
                        for idx, v in enumerate(varNames):
                            if v != timeColumnName:
                                darray = dataset[v]
                                experimentVars = extractCoordinates(file)
                                darray.loc[experimentVars] = data[:, idx].A1
                    # Fold the dataset along the seed variables, producing the mean and stdev datasets
                    mergingVariables = [seed for seed in seedVars if seed in dataset.coords]
                    means[experiment] = dataset.mean(dim = mergingVariables, skipna=True)
                    stdevs[experiment] = dataset.std(dim = mergingVariables, skipna=True)
            # Save the datasets
            pickle.dump(means, open(pickleOutput + '_mean', 'wb'), protocol=-1)
            pickle.dump(stdevs, open(pickleOutput + '_std', 'wb'), protocol=-1)
            pickle.dump(newestFileTime, open('timeprocessed', 'wb'))
    else:
        means = { experiment: xr.Dataset() for experiment in experiments }
        stdevs = { experiment: xr.Dataset() for experiment in experiments }

    # QUICK CHARTING

    import matplotlib
    import matplotlib.pyplot as plt
    import matplotlib.cm as cmx
    matplotlib.rcParams.update({'axes.titlesize': 12})
    matplotlib.rcParams.update({'axes.labelsize': 10})
    def make_line_chart(
        xdata,
        ydata,
        title=None,
        ylabel=None,
        xlabel=None,
        colors=None,
        linewidth=1,
        error_alpha=0.2,
        figure_size=(6, 4)
    ):
        fig = plt.figure(figsize = figure_size)
        ax = fig.add_subplot(1, 1, 1)
        ax.set_title(title)
        ax.set_xlabel(xlabel)
        ax.set_ylabel(ylabel)
#        ax.set_ylim(0)
#        ax.set_xlim(min(xdata), max(xdata))
        index = 0
        for (label, (data, error)) in ydata.items():
#            print(f'plotting {data}\nagainst {xdata}')
            lines = ax.plot(xdata, data, label=label, color=colors(index / (len(ydata) - 1)) if colors else None, linewidth=linewidth)
            index += 1
            if error is not None:
                last_color = lines[-1].get_color()
                ax.fill_between(
                    xdata,
                    data+error,
                    data-error,
                    facecolor=last_color,
                    alpha=error_alpha,
                )
        return (fig, ax)
    def generate_all_charts(means, errors = None, basedir=''):
        viable_coords = { coord for coord in means.coords if means[coord].size > 1 }
        for comparison_variable in viable_coords - {timeColumnName}:
            mergeable_variables = viable_coords - {timeColumnName, comparison_variable}
            for current_coordinate in mergeable_variables:
                merge_variables = mergeable_variables - { current_coordinate }
                merge_data_view = means.mean(dim = merge_variables, skipna = True)
                merge_error_view = errors.mean(dim = merge_variables, skipna = True)
                for current_coordinate_value in merge_data_view[current_coordinate].values:
                    beautified_value = beautifyValue(current_coordinate_value)
                    for current_metric in merge_data_view.data_vars:
                        title = f'{label_for(current_metric)} for diverse {label_for(comparison_variable)} when {label_for(current_coordinate)}={beautified_value}'
                        for withErrors in [True, False]:
                            fig, ax = make_line_chart(
                                title = title,
                                xdata = merge_data_view[timeColumnName],
                                xlabel = unit_for(timeColumnName),
                                ylabel = unit_for(current_metric),
                                ydata = {
                                    beautifyValue(label): (
                                        merge_data_view.sel(selector)[current_metric],
                                        merge_error_view.sel(selector)[current_metric] if withErrors else 0
                                    )
                                    for label in merge_data_view[comparison_variable].values
                                    for selector in [{comparison_variable: label, current_coordinate: current_coordinate_value}]
                                },
                            )
                            ax.set_xlim(minTime, maxTime)
                            ax.legend()
                            fig.tight_layout()
                            by_time_output_directory = f'{output_directory}/{basedir}/{comparison_variable}'
                            Path(by_time_output_directory).mkdir(parents=True, exist_ok=True)
                            figname = f'{comparison_variable}_{current_metric}_{current_coordinate}_{beautified_value}{"_err" if withErrors else ""}'
                            for symbol in r".[]\/@:":
                                figname = figname.replace(symbol, '_')
                            fig.savefig(f'{by_time_output_directory}/{figname}.pdf')
                            plt.close(fig)
    for experiment in experiments:
        current_experiment_means = means[experiment]
        current_experiment_errors = stdevs[experiment]
        print(current_experiment_means)
        #generate_all_charts(current_experiment_means, current_experiment_errors, basedir = f'{experiment}/all')
    
    gradient = means['gradient']
    gradient_error = stdevs['gradient']
    channel = means['channel']
    channel_error = stdevs['channel']
    basedir = f'{experiment}/all'
    
    dfs = {}
    for experiment in experiments:
        current_means = means[experiment]
        current_std = stdevs[experiment]
        #current_with_confidence = [current_means]
        current_with_confidence = [current_means, current_means + current_std, current_means - current_std]
        
        current_dfs = [ x.to_dataframe() for x in current_with_confidence ]
        current_df = pd.concat(current_dfs).reset_index()
        current_df = current_df[current_df['delay'] == 10]
        current_df['throttle'] = 1 / current_df['throttle']
        current_df.loc[current_df['mode'] == 'reactive', ['throttle']] = 0
        current_df.rename(
            columns = {
                'messages[sum]' : '# messages', 
                'root[mean]' : 'output (mean)' ,
                "source_gradient[sum]" : '# G[source]',
                "destination_gradient[sum]" : '# G[destination]',
            }, 
            inplace=True,
        )
        current_df.loc[current_df['mode'] == 'proactive', 'mode'] = "round"
        dfs[experiment] = current_df
   
    def produce_plot_for_simple_comparison(name):
        sns.set(font_scale=1.8)
        basedir = f'{name}'
        base_folder = f'{output_directory}/{basedir}'
        Path(base_folder).mkdir(parents=True, exist_ok=True)
        df = dfs[name]
        df = df[df['mode'] != "round"]
        
        ax = sns.lineplot(data=df, x="time", y="# messages", hue="throttle", style="mode", palette='tab10')
        sns.move_legend(ax, "upper left", bbox_to_anchor=(1, 1))
        fig = ax.get_figure()
        fig.savefig(f'{base_folder}/{name}-messages.pdf', bbox_inches='tight', pad_inches=0)
        
        fig.clf()
        ax = sns.lineplot(data=df, x="time", y="output (mean)", hue="throttle", style="mode", palette='tab10')
        sns.move_legend(ax, "upper left", bbox_to_anchor=(1, 1))
        fig.savefig(f'{base_folder}/{name}-root.pdf', bbox_inches='tight', pad_inches=0)
        fig.clf()
        
        df = dfs[name]
        df = df.drop(df[(df['mode'] == "reactive")].index)
        df = df.drop(df[(df['mode'] == "round") & (df['throttle'] != 1)].index)
        ax = sns.lineplot(data=df, x="time", y="# messages", hue="throttle", style="mode", palette='viridis')
        fig.savefig(f'{base_folder}/{name}-messages-round.pdf', bbox_inches='tight', pad_inches=0)
        fig.clf()
        
        ax = sns.lineplot(data=df, x="time", y="output (mean)", hue="throttle", style="mode", palette='viridis')
        fig.savefig(f'{base_folder}/{name}-messages-round.pdf', bbox_inches='tight', pad_inches=0)
        fig.clf()
        
        df = dfs[name]
        df = df.drop(df[(df['mode'] == "reactive")].index)
        ax = sns.lineplot(data=df, x="time", y="# messages", hue="throttle", style="mode", palette='viridis')
        fig = ax.get_figure()
        fig.savefig(f'{base_folder}/{name}-messages-round.pdf', bbox_inches='tight', pad_inches=0)    
        fig.clf()
        
        df = dfs[name]
        df = df.drop(df[(df['mode'] == "round") & (df['throttle'] != 1.0)].index)
        ax = sns.relplot(data=df, x="time", y="output (mean)", hue="throttle", col="mode", kind="line", palette='tab10')
        fig = ax.fig
        fig.savefig(f'{base_folder}/{name}-output-rel.pdf', bbox_inches='tight', pad_inches=0)
        fig.clf()
        ax = sns.relplot(data=df, x="time", y="# messages", hue="throttle", col="mode", kind="line", palette='tab10')
        for axes in ax.axes.flat:    
            axes.ticklabel_format(axis="y", style="sci", scilimits=(2, 3))
        fig = ax.fig
        fig.savefig(f'{base_folder}/{name}-messages-rel.pdf', bbox_inches='tight', pad_inches=0)
        fig.clf()
        
    for experiment in experiments:
        produce_plot_for_simple_comparison(experiment)
    
    sns.set(font_scale=1.8)
    # Channel
    df = dfs['channel']
    df = df[df['mode'] == 'throttle']
    df = df[df['throttle'] == 1]
    #df = df.drop(df[(df['mode'] == "reactive")].index)
    destination = df[["# G[destination]", "time", 'throttle']]
    destination = destination.rename(columns={"# G[destination]" : "# evaluations"}) 
    destination["subprogram"] = "G[destination]"
    source = df[["# G[source]", "time", 'throttle']]
    source["subprogram"] = "G[source]"
    source = source.rename(columns = {"# G[source]" : "# evaluations"})
    programs = [source, destination]
    program_pd = pd.concat(programs).reset_index()
    ax = sns.lineplot(data=program_pd, x="time", y="# evaluations", hue="subprogram")
    fig = ax.get_figure()
    fig.savefig(f'{output_directory}/message_count.pdf', bbox_inches='tight', pad_inches=0)
# Custom charting
