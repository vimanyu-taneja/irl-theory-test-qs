(ns irl-theory-test-qs.core
  (:require [clj-http.client :as client]
            [clojure.string :as str]
            [net.cgrand.enlive-html :as html]))

(def base-url "http://theory-tester.com/questions/")

(defn extract-text
  "Extract the text from a HTML element and sanitise it"
  [node]
  (-> (html/text node)
      (str/replace #"\n|\t" "")
      (str/replace #"\?s" "'s")))

(def parsers
  {:category    {:selector  [:.p-questionSingle-heading]
                 :processor #(-> (first %)
                                 (extract-text))}
   :question    {:selector  [:.pageHeading]
                 :processor #(-> (first %)
                                 (extract-text))}
   :image       {:selector  [:.quiz-question-img :> :img]
                 :processor #(-> (first %)
                                 (get-in [:attrs :src]))}
   :options     {:selector  [:.options-single]
                 :processor #(-> (map extract-text %))}
   :answer      {:selector  [:.js-correct-answer]
                 :processor #(-> (first %)
                                 (extract-text))}
   :explanation {:selector  [:.p-questionSingle-explanation]
                 :processor #(-> (first %)
                                 (extract-text)
                                 (str/replace #"Explantion:\s*" ""))}})  ;; sic

(defn scrape-question
  "Construct a map of all the data for a single question"
  [question-num]
  (let [url         (str base-url question-num)
        parsed-html (-> (client/get url)
                        (:body)
                        (html/html-snippet))]
    (reduce (fn [result [field {:keys [selector
                                       processor]}]]
              (assoc result
                     field (-> (html/select parsed-html selector)
                               (processor))))
            {:id question-num}
            parsers)))

(defn -main [& args]
  (println "Running with args:" args))
