# short-string-compression-demo

Clojure files to produce a PNG chart of string lengths, compressed vs uncompressed.

`resources/comments.edn` contains anonymized comment data from soundcloud.

## Usage

    $ lein repl
    (scatter' comments [gzip deflate] ["gzip" "deflate"])

## License

Copyright © 2016 Ben Cook

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
