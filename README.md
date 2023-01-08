# search.morling.dev

The search feature for the https://morling.dev website. It is made up of two parts:

* A Quarkus extension for Apache Lucene
* A Quarkus application which contains an index of morling.dev and exposes it via Lucene

See instructions in _search-morling-dev_ for how to build the search application.

## License

The content of this repository is generally licensed under the Apache License, version 2.
The only exception is the file _search-morling-dev/src/main/resources/META-INF/searchindex.json_,
which is licensed under Creative Commons BY-SA 4.0.
