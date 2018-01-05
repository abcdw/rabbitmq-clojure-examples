(ns rabbitmq-clojure-examples.core
  (:require [langohr.channel :as lch]
            [langohr.consumers :as lc]
            [langohr.core :as rmq]
            [langohr.basic :as lb]
            [langohr.exchange :as le]
            [langohr.queue :as lq]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

;; langohr.core/*default-config*
;; => {:username "guest", :password "guest", :vhost "/", :host "localhost", :port 5672}

(def ^{:const true}
  default-exchange-name "")

(defn message-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (println (format "[consumer] Received a message: %s, delivery tag: %d, content type: %s, type: %s"
                   (String. payload "UTF-8") delivery-tag content-type type)))

(defn connect-rabbitmq []
  (let [conn    (rmq/connect)
        channel (lch/open conn)
        qname   "queue.test"]
    (println (format "[main] Connected. Channel id: %d" (.getChannelNumber channel)))
    (lq/declare channel qname {:exclusive false
                               :auto-delete true})
    (lc/subscribe channel qname message-handler {:auto-ack true})
    (lb/publish channel default-exchange-name qname "Hello!"
                {:content-type "text/plain" :type "greetings.hi"})
    (Thread/sleep 2000)
    (println "Connection closed")
    (lch/close channel)
    (rmq/close conn)))

(defonce connection (rmq/connect))
(def channel (lch/open connection))

(lq/declare channel "queue.test" {:exclusive false
                                  :auto-delete true})

(le/declare channel "test-exchange" "fanout" {:durable false
                                              :auto-delete true})

(lq/bind channel "queue.test" "test-exchange")
(lc/subscribe channel "queue.test" message-handler)
(lb/publish channel "test-exchange" "" "test-message")

;; (connect-rabbitmq)
