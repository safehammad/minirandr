(ns com.safehammad.minirandr
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [clojure.java.shell :as shell]
            [clojure.tools.cli :as cli])

  (:gen-class))

(def version-string "0.1.1")

(defn ->screen
  "Create screen entity."
  [[screen connected & [resolution]]]
  (let [connected? (= connected "connected")]
    {:screen screen
     :connected connected?
     :resolution (when connected? resolution)}))

(defn parse-xrandr
  "Parse xrandr output to return a sorted screen map of {screen-name connected resolution, ...}.

  For example: {0 {:screen \"eDP1\", :connected true :resolution \"1920x1080\"}
                1 {:screen \"DP1\", :connected true :resolution \"1920x1200\"}
                2 {:screen \"HDMI1\", :connected false :resolution nil}"
  [xrandr-output]
  (into {} (->> (str/split-lines xrandr-output)
                (map str/trim)
                (partition-all 2 1)
                (filter #(re-matches #".* (connected|disconnected) .*" (first %)))
                (map (partial map #(str/split % #" ")))
                (map (partial mapcat (partial take 2)))
                (map ->screen)
                (sort-by (comp not :connected))  ; group connected items {:connected true} first
                (map-indexed vector))))

(comment
  (parse-xrandr
  "eDP1 connected primary 1920x1080+0+0 (normal left inverted right x axis y axis) 310mm x 170mm
   1920x1080     60.00*+  59.93
   1680x1050     59.88
   1400x1050     59.98
   1600x900      60.00    59.95    59.82
   1280x1024     60.02
   1400x900      59.96    59.88
   1280x960      60.00
   1368x768      60.00    59.88    59.85
   1280x800      59.81    59.91
   1280x720      59.86    60.00    59.74
   1024x768      60.00
   1024x576      60.00    59.90    59.82
   960x540       60.00    59.63    59.82
   800x600       60.32    56.25
   864x486       60.00    59.92    59.57
   640x480       59.94
   720x405       59.51    60.00    58.99
   640x360       59.84    59.32    60.00
DP1 disconnected (normal left inverted right x axis y axis)
DP2 disconnected (normal left inverted right x axis y axis)
HDMI1 disconnected (normal left inverted right x axis y axis)
HDMI2 connected primary 1920x1080+0+0 (normal left inverted right x axis y axis) 530mm x 300mm
   1920x1080     60.00*+  74.97    50.00    59.94
   1920x1080i    60.00    50.00    59.94
   1680x1050     59.88
   1280x1024     75.02    60.02
   1440x900      59.90
   1280x960      60.00
   1280x720      60.00    50.00    59.94
   1024x768      75.03    70.07    60.00
   832x624       74.55
   800x600       72.19    75.00    60.32    56.25
   720x576       50.00
   720x480       60.00    59.94
   640x480       75.00    72.81    66.67    60.00    59.94
   720x400       70.08
VIRTUAL1 disconnected (normal left inverted right x axis y axis)"))

(defn print-screens
  "Return string formatted 0-based indexed list of screens to choose from."
  [screen-map]
  (str/join "\n" (for [[i {:keys [screen connected resolution] }] screen-map
                       :when connected]
                   (str i ": " screen " " resolution))))

(defn make-screen-choice
  "Given command line args, return set of screen-indexes chosen in order, together with whether they're primary.
  For example: 2p 0 = ({:screen-index \"2\" :primary true} {:screen-index \"0\" :primary false})"
  [args]
  (map
    #(hash-map :screen-index (Integer/parseInt (subs % 0 1))
               :primary (= (str/lower-case (subs % 1)) "p")
               :off false)
    args))

(defn make-off-screens
  "Given a map of screens and the choice of screens to turn on, return a map of screens to switch off."
  [screen-map screen-choice]
  (let [chosen-screen-indexes (map :screen-index screen-choice)
        all-screen-indexes    (keys screen-map)
        off-screen-indexes    (remove (set chosen-screen-indexes) all-screen-indexes)]
    (map
      #(hash-map :screen-index %
                 :primary false
                 :off true)
      off-screen-indexes)))

(defn extend-command
  "Extend the xrandr command with the given screen config."
  [cmd screen off primary previous-screen]
  (let [cmd (conj cmd "--output" screen)]
    (if off
      (conj cmd "--off")
      (cond-> (conj cmd "--auto" )
        primary         (conj "--primary")
        previous-screen (conj "--right-of" previous-screen)))))

(defn format-xrandr-cmd
  "Create the xrandr command to execute as a vector of strings."
  [screen-map screen-choice off-screens]
  (loop [cmd             ["xrandr"]
         screen-config   (concat screen-choice off-screens)
         previous-screen nil]
    (if (seq screen-config)
      (let [{:keys [screen-index primary off]} (first screen-config)
            {:keys [screen]}                   (get screen-map screen-index)
            cmd                                (extend-command cmd screen off primary previous-screen)]
        (recur cmd (rest screen-config) screen))
      cmd)))

(defn run-xrandr!
  "Format and run the xrandr command."
  [screen-map args]
  (let [screen-choice (make-screen-choice args)
        off-screens   (make-off-screens screen-map screen-choice)
        cmd           (format-xrandr-cmd screen-map screen-choice off-screens)
        result        (apply shell/sh cmd)]
    (str (str/join " " cmd) "\n" (:err result))))

(map (comp second (partial re-matches #"(\d+)p?")) (str/split (str/trim "x 2p") #"\s+" ))

(defn connected-screen-indexes
  "Return a set of indexes of connected screens as strings."
  [screen-map]
  (set (->> screen-map
            (filter (comp :connected val))
            (map (comp str key)))))

(defn valid-args?
  "Ensure indexes parsed as args correspond to connected screens."
  [screen-map args]
  (let [connected-indexes (connected-screen-indexes screen-map)
        parsed-args       (map (comp second (partial re-matches #"(\d+)p?")) args)]
    (and
      (seq parsed-args)
      (every? some? parsed-args)
      (set/subset? (set parsed-args) connected-indexes))))

(defn screen-map!
  "Run xrandr command to return a screen map."
  []
  (-> (shell/sh "xrandr") :out parse-xrandr))

;; Command line entrypoint

(def usage "Usage: minirandr [OPTION] [SCREEN]...")

(def instructions "First list all connected screens by running:

    $ minirandr -l  # This is the same as running minirandr with no arguments

Connected screens will be listed by index detailing their name and resolution, for example:

    0: eDP1 1920x1080
    1: HDMI2 2560x1080

To configure screens, provide screen specs as indexes in the order that they are positioned, left to right. An optional `p` can be added to an index to make a screen primary. For example, to configure screen 1 to be left of screen 0, and for screen 0 to be primary, run:

    $ minirandr 1 0p

To quickly configure screen 0 as the single primary screen, run:

    $ minirandr -s")

(def cli-opts
  [["-l" "--list-screens" "List connected screens"]
   ["-s" "--single-screen" "Configure first screen listed as the only screen and set as primary"]
   ["-v" "--version" "Display version"]
   ["-h" "--help" "Display help"]])

(defn main!
  "Main entry point."
  [args]
  (let [parsed-opts                                (cli/parse-opts args cli-opts)
        {:keys [options arguments errors summary]} parsed-opts
        {:keys [list-screens single-screen version help]}  options
        list-screens                               (or list-screens (every? empty? [options arguments]))
        screen-map                                 (screen-map!)]
    (cond
      errors                              (str/join "\n" errors)
      help                                (str/join "\n\n" [usage summary instructions])
      version                             (str "minirandr v" version-string)
      list-screens                        (print-screens screen-map)
      single-screen                       (run-xrandr! screen-map ["0p"])
      (not (valid-args? screen-map args)) (str "Invalid screen specs. "
                                               "The following screens are available:\n\n"
                                               (print-screens screen-map)
                                               "\n\nPlease run `minirandr --help` for more information.")
      :else                               (run-xrandr! screen-map arguments))))

(defn -main
  [& args]
  ;(println (screen-map!))
  (println (main! args))
  (System/exit 0))
