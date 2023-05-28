(ns irl-theory-test-qs.core
  (:require [clj-http.client :as client]
            [clojure.string :as str]
            [net.cgrand.enlive-html :as html]))

(def base-url "http://theory-tester.com/questions/")

(defn get-max-value
  "Get the max value of selected nodes' content (integers) on a page"
  [url selector]
  (let [all-nodes (->  (client/get url)
                       (:body)
                       (html/html-snippet)
                       (html/select selector))
        max-value (->> all-nodes
                       (keep
                        #(try (->> (:content %)
                                   (first)
                                   (Integer/parseInt))
                              (catch NumberFormatException _
                                nil)))
                       (apply max))]
    max-value))

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

(defn -main [& _]
  (let [last-page-num     (get-max-value base-url [:.pagination :li :a])
        last-page-url     (str base-url "?page=" last-page-num)
        last-question-num (get-max-value last-page-url [:.questionList-single-numbering :h3])]))
