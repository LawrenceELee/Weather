Android app to get weather information for a city.

Learned:
* REST web services:
    + Representational state transfer means that server/API doesn't "remeber" any information from previous sessions.
    + We have to provide the "state" with every transaction (i.e. send tokens or cookies, etc).
    + More info from wikipedia page: a network of web pages (a virtual state-machine), where the user progresses through the application by selecting links (state transitions), resulting in the next page (representing the next state of the application) being transferred to the user and rendered for their use.
* AsyncTask:
    + have to run separate thread to do tasks that might take "a while" so it doesn't interupt the main UI thread (i.e. so that main UI thread remains responsive). 
    + Every time an AsyncTask is required, we must create a new instance of AsyncTask, since each instance can be execute() only once.
    + AsyncTasks handle the details of creating threads and executing its methods on the appropriate threads for you so you.
* HttpUrlConnection - used to create a HTTP request to weather API.
* Parsing JSON (JSONObject, JSONArray) - HTTP response will be in JSON (Javascript Object Notation), we have to parse this data to get the information we want.
* ListViews - used to display the JSON data in a "human readable" list form.
* ArrayAdapters - adapters are a pattern used to transform from array to ListViwe.
* ViewHolder Pattern: 
    + reuse views that scroll off the screen in a ListView, rather than creating new views.
    + inflate() and findViewById() calls are expensive, and ViewHolder pattern only does those operations when we are creating a ListView for the first time, otherwise we'll reuse the ListView that scrolled off the screen.
* View widgets: 
    + TextInputLayout - similar to the "hint" property of an EditText, but hint persists when user inputs text.
    + SnackBar - like a Toast notification but can be interacted with (e.g. swipe, etc).
    + FloatingActionButton - instead of a seperate "okay" or "enter" button. Floating means it is "more important" than other widgets.
