# CustomCamera

A camera(custom) app that does the following:

Screen 1:
Takes user input (name and email) and stores it in firebase realtime DB.
Prepopulates the fields if once already entered ealier.

Screen 2:
Captures an image with certain params and then the countdown timer starts for 300 seconds.
*Although the camera view looks rotated by 90 deg, they are saved with proper orientation in Phone gallery.

Screen 3:
Clicks 25 images with Exposure Values ranging from -12 to 12 and saves them in Gallery.
Then it chooses the image whose EV is closes to the mean of the EVs of all the photos,
and sends it to the db (through two API calls).
