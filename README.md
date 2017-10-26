Feature presentation

* Review code commit by commit
* Add a comment on line code
* Approve a commit
* Git and SVN support


Installation notes:

* adjust `configuration.example` as instructed in the file and rename it to `configuration.json`
* install Maven and build the project using `mvn clean package`
* copy `cordonbleu-main-0.0.1-SNAPSHOT.jar` from `cordonbleu-main/target` into this folder
* install MongoDB
* run `run.sh`
* view `http://localhost:8080/`
* register as a new user
* give yourself admin permissions:
  - log into MongoDB
  - `use cordonbleu`
  - `db.user.find().pretty()` and note the `_id` of your user
  - `db.user.update({ _id: "[INSERT ID HERE]" }, { $set: { flags: ["ADMIN"] } })` inserting the `_id` from the previous step

Development notes:

* install node + npm
* `cd` to `cordonbleu-main`
* `npm install`
* `npm install webpack -g`
* `webpack --watch` will auto-compile everything under `src/main/resources/webpack` and bundle it into `target/classes/static/js/bundle.js` which is referenced in `index.html`
* start the `CordonBleuApplication` dropwizard server using `server src/test/resources/config-test.json` as arguments

Now you can develop, all changes in `src/main/resources/webpack` will be bundled automatically upon file-save. `CordonBleuApplication` needs to be restarted if backend changes are made though.

SASS features used:

* Variables: Declare using `$var: 123px;` just like you would usually declare a CSS property.
* Calculations: Combined with variables, you can calculate CSS properties, e.g. `top: $navbarHeight + 3px;`.

ES6 features used:

* Arrow functions: Same semantics as in Java just with `=>` instead of `->`. The `this` variable is not affected in contrast to `function`s and keeps its scope, which is very handy in most scenarios.
  * Just like in Java, singular expressions don't need to be wrapped by `{}` and `return` can be omitted. If you want to return an object as a singular expression, wrap it with `()`.
* Spread operator `...array` to expand an array into the arguments of a function.

ES6 features that sound useful (but haven't been used yet):

* Using `const` and `let` instead of `var`.
* Appending `,` to the last element of an array or object. It's allowed by ES6 and is easier to refactor this way.
* Using backticks ``` to reference variables within strings. Quit using `+` for string concatenation.
* Default parameter values.
* Code Style guidelines for ES6 with some interesting ideas: https://github.com/elierotenberg/coding-styles/blob/master/es6.md
