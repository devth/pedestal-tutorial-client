(ns tutorial-client.simulated.start
  (:require [io.pedestal.app.render.push.handlers.automatic :as d]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [tutorial-client.start :as start]
            [tutorial-client.simulated.services :as services]
            [tutorial-client.rendering :as rendering]
            [goog.Uri]
            ;; This needs to be included somewhere in order for the
            ;; tools to work.
            [io.pedestal.app-tools.tooling :as tooling]))

(defn param [name]
  (let [uri (goog.Uri. (.toString  (.-location js/document)))]
    (.getParameterValue uri name)))

(defn ^:export main []
  (let [app (start/create-app d/data-renderer-config)
        services (services/->MockServices (:app app))]
    (app/consume-effects (:app app) services/services-fn)
    (p/start services)
    app))
