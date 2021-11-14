(ns com.safehammad.minirandr-test
  (:require [clojure.test :refer [deftest is are testing]]
            [com.safehammad.minirandr :as minirandr]
            [clojure.string :as str]))

(def sample-xrandr-output
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
    VIRTUAL1 disconnected (normal left inverted right x axis y axis)")

(def sample-screen-map {0 {:screen "eDP1", :connected true, :resolution "1920x1080"},
                        1 {:screen "HDMI2", :connected true, :resolution "1920x1080"},
                        2 {:screen "DP1", :connected false, :resolution nil},
                        3 {:screen "DP2", :connected false, :resolution nil},
                        4 {:screen "HDMI1", :connected false, :resolution nil},
                        5 {:screen "VIRTUAL1", :connected false, :resolution nil}})

(def sample-screen-choice [{:primary false, :screen-index 0} {:primary true, :screen-index 1}])  ; Args: 0 1p

(def sample-off-screens [{:screen-index 2, :off true}  ; Args: 0 1p
                         {:screen-index 3, :off true}
                         {:screen-index 4, :off true}
                         {:screen-index 5, :off true}])

(def sample-xrandr-cmd
  "xrandr --output eDP1 --auto --output HDMI2 --auto --primary --right-of eDP1 --output DP1 --off --output DP2 --off --output HDMI1 --off --output VIRTUAL1 --off")

(deftest parse-xrandr-test
  (is (= sample-screen-map (minirandr/parse-xrandr sample-xrandr-output))))

(deftest valid-args-test
  (testing "Valid args"
    (are [args] (minirandr/valid-args? sample-screen-map args)
         ["0"]     ; connected index
         ["1"]     ; another connected index
         ["0p"]    ; set as primary
         ))
  (testing "Invalid args"
    (are [args] (not (minirandr/valid-args? sample-screen-map args))
         []        ; args must exist - we won't switch off all screens!
         ["x"]     ; not a screen index
         ["0x"]    ; suffix can only be p
         ["2"]     ; screen not connected
         ["6"])))  ; screen index out of range

(deftest connected-screen-indexes-test
  (is (= #{"0" "1"} (minirandr/connected-screen-indexes sample-screen-map))))  ; Args: 0 1p

(deftest make-screen-choice-test
  (is (= sample-screen-choice (minirandr/make-screen-choice ["0" "1p"]))))

(deftest off-screens-test
  (is (= sample-off-screens (minirandr/make-off-screens sample-screen-map sample-screen-choice))))

(deftest format-xrandr-cmd-test
  (is (= sample-xrandr-cmd (str/join " " (minirandr/format-xrandr-cmd sample-screen-map sample-screen-choice sample-off-screens)))))
