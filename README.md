# Running

0. Clone the repository.
1. Import the gradle project to your favourite IDE (tested with IntelliJ).
2. Fetch the dependencies (should happen automatically).
3. Rename the file `quedex-config.properties.example` in `qdx-market-maker/src/main/resources` to `quedex-config.properties` and fill in your details.
4. Change the configuration of the bot in the `Main` class (located in `qdx-market-maker/src/main/java/com/quedex/marketmaker`) according to you liking (the configuration will be moved to a `.properties` file soon).
5. Run the `Main` class.

# Features

The market making bot:
* places orders with configurable quantities on configurable number of levels
* has configurable spread
* follows a predefined Fair Price (currently last price or mid - change the implementation in the `MarketMaker` class between `LastFairPriceProvider` and `MidFairPriceProvider`)
* has configurable risk management - stops quoting one side of the order book when delta limit exceeded
* cancels all orders when going down or on error
* is easily extensible

TODO:
* sensitivity to Fair Price changes
* options market making
* runnable .jar
* config file
* logging configuration
* documentation
* websockets API
