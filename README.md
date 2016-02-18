# Running
(requires Java 8; does not require gradle installation - uses gradle wrapper)

## Standalone JAR
(the following steps are for Linux, tested on Ubuntu 15.10)

1. Execute `./gradlew shadowJar` from the main project directory (`qdx-market-maker`).
2. Jar will be created in `qdx-market-maker/build/libs/` named `qdx-market-maker-<version>-all.jar`.
3. Copy the jar to a convenient location, place your `quedex-config.properties` and `market-maker.properties`
 (examples may be found in `qdx-market-maker/src/main/resources`) next to it.
4. Run the jar with `java -jar qdx-market-maker-<version>-all.jar quedex-config.properties market-maker.properties`. To
 exit hit CTRL + C.

## From an IDE

0. Clone the repository.
1. Import the gradle project to your favourite IDE (tested with IntelliJ).
2. Fetch the dependencies (should happen automatically).
3. Rename the file `quedex-config.properties.example` in `qdx-market-maker/src/main/resources` to 
`quedex-config.properties` and fill in your details.
3. Rename the file `market-maker.properties.example` in `qdx-market-maker/src/main/resources` to 
`market-maker.properties` and change the configuration according to your liking.
5. Run the `Main` class.

# Features

The market making bot:
* places orders with configurable quantities on configurable number of levels
* has configurable spread
* follows a predefined Fair Price (currently last price or mid - change the implementation in the `MarketMaker` class 
between `LastFairPriceProvider` and `MidFairPriceProvider`)
* has configurable risk management - stops quoting one side of the order book when delta limit exceeded
* cancels all orders when going down or on error
* is easily extensible

TODO:
* sensitivity to Fair Price changes
* options market making
* logging configuration
* documentation
* websockets API
