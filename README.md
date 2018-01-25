# stock-chart
This is one of my very first clojure apps that I used to teach myself basics of Clojure and ClojureScript. 
The app is designed to chart closing prices of a stock (on a line graph) for 20 days before a quarterly earnings release and for 30 days
after an earnings release. Additionally, it pulls in closing price data for the previous 4 years so that you can visually
see how a stock's price moves in previous quarters. Once the data is plotted on a graph, you would use it to spot any movement trends of a stock
in and out of a quarterly earnings release. 
Maybe WalMart always move up 2 dollars before its Q1 release. Maybe Amazon always goes down 15 dollars during the five days before it Q3 release.

<b>Note:</b> The datasources used by this app have gone down and/or are no longer free. 
I'll have to scour the web for another free source.
Until that time, the app will only allow you to view data for Amazon(AMZN), as I have to hard code earnings release dates,
into the app 

## Libraries
The following clojure libraries were used:
* [clojurescript](https://github.com/clojure/clojurescript) - in lieu of writing any javascript
* [austin](https://github.com/cemerick/austin) - for its clojurescript browser REPL
* [enlive](https://github.com/cgrand/enlive) - to scrape earnings release dates from Briefing.com
* [clojure-csv](https://github.com/davidsantiago/clojure-csv) - to load and parse csv files returned from Yahoo Finance
* [compojure](https://github.com/weavejester/compojure) - to process and route HTTP requests within my application
* [ring-json-response](https://github.com/weavejester/ring-json-response) - to convert clojure maps into JSON
* [cljs-ajax](https://github.com/JulianBirch/cljs-ajax) - to receive and send JSON responses over AJAX
* [domina](https://github.com/levand/domina) - to perform DOM manipulations
* [rickshaw.js](http://code.shutterstock.com/rickshaw/) - to create simple javascript line graphs

## Datasources
Historical closing price data for a stock is pulled from Yahoo Finance's free (for now) HTTP API.
Earnings release date data is HTML scraped from Briefing.com, this site is currently down.  

## Usage
After cloning the repository, go into the stock-chart project directory, and use Leiningen to run
1. ```lein clean``` then ```lein cljsbuild once``` then ```lein compile``` and then ```lein repl```
2. at the user=> prompt type ```(run)``` to start Ring's local embedded Jetty server at (default) localhost:3000
3. go to http://localhost:3000 on your browser, a very simple HTML form should appear that allows you to enter a ticker
symbol and select an earnings release date.
4. click the submit button to generate a line graph of the price movement around the earnings release date.

## Screenshot
Screenshot of the line chart generated for Amazon.com, ticker (AMZN), The day of the earnings release is marked as 
"D0" on the charts x-axis
![alt text](https://github.com/strohs/stock-chart/blob/master/Stock-Trends-Chart.jpg "Line Chart screenshot")

## License
Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
