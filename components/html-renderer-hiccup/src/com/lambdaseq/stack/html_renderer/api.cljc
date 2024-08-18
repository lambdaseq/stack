(ns com.lambdaseq.stack.html-renderer.api
  (:require [com.lambdaseq.stack.html-renderer.core :as core]))

(defmacro render [html]
  (core/render html))
