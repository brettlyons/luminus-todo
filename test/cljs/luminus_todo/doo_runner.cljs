(ns luminus-todo.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [luminus-todo.core-test]))

(doo-tests 'luminus-todo.core-test)

