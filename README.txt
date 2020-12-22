MOON TRACKER

This is the README to the MOON TRACKER web app, created by Alek Metzelaar, Kavel Rao, and Adam Wang for CSE 143 at the University of Washington.
Please see below for a guide to using this web app, or see here for a video guide: https://youtu.be/I0i6pxA9L3U.

Moon Tracker is a tool to assist in taking landscape photos of the moon behind a landmark, like the Space Needle in Seattle.
Since the moon's position is constantly changing, it's not easy to figure out where to take such a photo from.
We only need a few inputs, and the web app will tell you exactly where to stand to snap your perfect photo.
Representation of Moon Tracker: https://i.imgur.com/rOmDb5l.png

USER GUIDE
Please use Google Chrome as your browser when using this program, as we cannot ensure compatibility with other browsers.

STEP 1: Compiling and running the web server
   1. Open an Ed terminal.
   2. Enter the following command into the terminal: javac Server.java && java Server
   3. Through the Ed "network" icon, click on the program listening on port 8001.
   4. You should now have a new tab open with the Moon Tracker web app running.

STEP 2: Using the web app
   1. Moon Tracker requires you to input a few pieces of information to use in our calculations.
   2. Input the height (in meters) of the target that you want to photograph.
   3. Latitude and longitude
      Next, we need the coordinates of your target. It's important to be as precise as possible with this,
      since small variations can greatly affect your results.
      For finding your target's latitude and longitude, you can use Google Maps or a tool like https://www.latlong.net/.
      3.1. Input the latitude of the target (North is positive, South is negative).
      3.2. Input the longitude of the target (East is positive, West is negative).
   4. Finally, input the time (and timezone) you want to take photos, so we can calculate the location of the moon.
      We recommend planning in the future so that you have time to get set up before the moon is in position.

STEP 3: Results
   There are two possible forms that your results could take.
   1. If at the time you input, the moon would not have risen at the location of the target,
      you will get a message on the web app suggesting to enter a different time.
      You can find the times of moonrise and moonset at your location using https://www.timeanddate.com/astronomy/,
      and input a time that falls between those times so that the moon will be in the sky.
   2. If the moon is above the horizon at the time you input, then you will see a location's coordinates in latitude and longitude as your result.
      This location should be the perfect spot for your picture! You can input this latitude and longitude straight into Google Maps or
      the map software of your choice. Try to get there a bit earlier than the time you input, so you have plenty of time to get set up. 
      Enjoy your photography!

