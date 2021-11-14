(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.java.shell :as shell]))

(def lib 'com.safehammad/minirandr)
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-standalone.jar" (name lib)))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'com.safehammad.minirandr}))

(defn native-image [_]
  (uber nil)
  (shell/sh "./build-native.sh" uber-file))
