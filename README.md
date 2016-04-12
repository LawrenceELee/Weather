Android app to get weather information for a city.

Learned:
* REST web services - Representational state transfer means that server/API doesn't "remeber" any information from previous sessions. We provide the "state" with every transaction. More info: a network of web pages (a virtual state-machine), where the user progresses through the application by selecting links (state transitions), resulting in the next page (representing the next state of the application) being transferred to the user and rendered for their use.
* AsyncTask - have to run separate thread to do tasks that might take "a while" so it doesn't interupt the main UI thread (i.e. so that main UI thread remains responsive).
* HttpUrlConnection - create a HTTP request to weather API.
* Parsing JSON (JSONObject, JSONArray) - HTTP response will be in JSON (Javascript Object Notation), we have to parse this data to get the information we want.
* ListViews - used to display the JSON data in a "human readable" list form.
* ArrayAdapters - adapters are a pattern used to transform from array to ListViwe.
* ViewHolder Pattern
