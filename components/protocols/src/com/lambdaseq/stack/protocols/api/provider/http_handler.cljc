(ns com.lambdaseq.stack.protocols.api.provider.http-handler)

(defprotocol IHttpHandlerProvider
  (get-handler [this]))
