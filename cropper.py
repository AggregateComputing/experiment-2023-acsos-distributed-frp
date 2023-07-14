import os
from PIL import Image

# Define the cropping region as a tuple (left, top, right, bottom)
cropping_region = (312, 0, 1175, 842)

# Loop through all the files in the current directory
for filename in os.listdir('./snapshots'):
    # Check if the file is an image
    if filename.endswith('.png'):
        print(filename)
        # Open the current image
        image = Image.open("./snapshots/" + filename)

        # Crop the image using the cropping region
        cropped_image = image.crop(cropping_region)

        # Save the cropped image to the same directory
        cropped_image.save("./snapshots/" + filename)