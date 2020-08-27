;; Copyright © 2020, JUXT LTD.

(ns juxt.mmxx.seeder
  (:require
   [integrant.core :as ig]
   [crux.api :as crux]
   [clojure.java.io :as io]
   [clojure.string :as str])
  (:import
   (java.util UUID)
   (java.net URI)))

(defmethod ig/init-key ::seeder [_ {:keys [crux]}]
  (crux/submit-tx
   crux
   (for [resource
         [
          ;; Can this be PUT by using a special content-type of application/vnc.crux.entity+edn ?
          ;; Or via the http-server?
          ;; See https://www.iana.org/assignments/media-types/media-types.xhtml
          ;; Note, we can also add Content-Location as a request header, which solves this problem.

          {:crux.db/id :spin/readme
           :juxt.http/uri (new URI "http://localhost:8082/spin/README")
           :juxt.http/variants
           [:spin/readme-adoc :spin/readme-html]
           :juxt.http/methods #{:get :options}}

          {:crux.db/id :spin/readme-adoc
           :juxt.http/uri (new URI "http://localhost:8082/spin/README.adoc")

           :content (slurp (io/file (System/getProperty "user.home") "src/github.com/juxt/spin/README.adoc"))
           :juxt.http/content-type "text/plain;charset=utf-8"
           ;; slightly privilege the raw adoc over the html
           :juxt.http/quality-of-source 0.9

           ;; The :put indicates this is a 'source' document - maybe we can use this?
           :juxt.http/methods #{:get :put :options}}

          {:crux.db/id :spin/readme-html
           :juxt.http/uri (new URI "http://localhost:8082/spin/README.html")

           :content "<h2>TODO: This will be the generated HTML 'type' of the content</h2>\n"
           :juxt.http/content-type "text/html;charset=utf-8"
           :juxt.http/quality-of-source 0.8

           :juxt.http/methods #{:get :options}}]]

     [:crux.tx/put
      (cond-> resource
        ;; Set the content length
        (:content resource) (assoc :juxt.http/content-length (count (:content resource))))])))