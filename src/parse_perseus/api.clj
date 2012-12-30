(ns parse_perseus.api
  (:use
   compojure.core
   hiccup.page-helpers
   ring.middleware.json-params
   ring.adapter.jetty
   sandbar.stateful-session)
  (:require
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [compojure.route :as route])
  (:import
   [org.codehaus.jackson JsonParseException]
;   [clojure.contrib.condition Condition]
    ))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)})

(def error-codes
  {:invalid 400
   :not-found 404})

(defn layout
  [body]
  (html5
   [:head]
   [:body
    body]))

(defn index-page
  []
  (layout [:p "hello again"]))

(defn wrap-error-handling [handler]
  (fn [req]
    (try
      (or (handler req)
          (json-response {"error" "resource not found"} 404))
      (catch JsonParseException e
        (json-response {"error" "malformed json"} 400))
      (catch Exception e
        (json-response {"error" "something went wrong"} 500)))))

(defn log-everything
  [handler]
  (fn [request]
    (let [resp (handler request)]
      (log/debug (format "request: %s" request))
      resp)))

(defroutes handler
  (GET "/" []
       (index-page))
  (route/files "/" {:root "resources/public"}))

(def perseus-app
  (-> handler
      wrap-json-params
      log-everything
      wrap-error-handling
      wrap-stateful-session))

(defn run-server
  [options]
  (run-jetty #'perseus-app options))

(defn bg-server
  []
  (run-server {:join false :port 5001}))

(defn -main
  [& args]
  (let [port (if (System/getenv "PORT")
               (Integer/parseInt (System/getenv "PORT"))
               5001)]
    (run-server {:port port})))
