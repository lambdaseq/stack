(ns com.lambdaseq.stack.api-router.core
  (:require [com.lambdaseq.stack.basic-http-router-reitit.api :as router]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [ring.util.http-response :as http]))

(defn make-router
  [routes]
  (router/make-router
    [["/api"
      routes
      ["/ping" {:get {:handler   (constantly (http/ok "pong"))
                      :summary   "Endpoint to ping server"
                      :responses {200 {:body [:enum "pong"]}}}}]
      ["/docs/*"
       {:get (swagger-ui/create-swagger-ui-handler
               {:url    "/api/swagger.json"
                :config {:validator-url nil}})}]
      ["/swagger.json"
       {:get {:no-doc  true
              :swagger {:info {:title       "API documentation"
                               :description "swagger.json for API"}}
              :handler (swagger/create-swagger-handler)}}]]]))
