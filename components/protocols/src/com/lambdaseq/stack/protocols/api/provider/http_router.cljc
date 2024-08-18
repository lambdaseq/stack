(ns com.lambdaseq.stack.protocols.api.provider.http-router)

(defprotocol IHttpRouterProvider
  (get-router [this])
  (get-routes [this]))
