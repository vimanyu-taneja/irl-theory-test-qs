(defproject irl-theory-test-qs "0.1.0-SNAPSHOT"
  :description "Extract questions for the Irish Driver Theory Test from the web."
  :url "https://github.com/vimanyu-taneja/irl-theory-test-qs"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[clj-http "3.12.3"]
                 [enlive "1.1.6"]
                 [org.clojure/clojure "1.11.1"]]
  :main ^:skip-aot irl-theory-test-qs.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :repl-options {:init-ns irl-theory-test-qs.core})
