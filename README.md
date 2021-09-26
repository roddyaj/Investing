# Investing

This program helps with managing stock and option positions within a portfolio.

*Note, this project is not yet ready for outside use, but it should become more user-friendly over time.*

## Features
- Maintaining position allocations to predetermined percentages, including the ability to nest desired allocations
- Identify candidate options to sell
- Identify candidate options to buy-to-close
- Visualize current option positions
- Track monthly option premium received
- Account data comes from Schwab exports
- Price data is pulled from from Finnhub and AlphaVantage

## Usage

Prerequisites: Ensure that Java (>= 16) and Maven are installed

Build: ```mvn compile```

Run: ```./run.sh run Combined <account-name>```

TODO: Instructions for creating settings.xml file

TODO: Instructions for exporting Schwab account files
