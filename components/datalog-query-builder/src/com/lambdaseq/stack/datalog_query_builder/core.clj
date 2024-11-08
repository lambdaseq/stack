(ns com.lambdaseq.stack.datalog-query-builder.core)

(defn keyword->datalog-query-symbol
  "Given a keyword, return a symbol that can be used in a datalog query.
  The symbol is prefixed with a question mark. Retains the keyword's name and namespace.
  The symbol is not namespaces, the namespace is in the name of the symbol with a dash, before the name of the keyword."
  [k]
  (let [n (namespace k)
        sym (cond-> "?"
              (not-empty n) (str n "-")
              :always (str (name k)))]
    (symbol sym)))

(defn build-clauses
  "Given a map of where clauses (each key is a property and each value is a value to match)
   build a map with :in and :where entries that matches the structure of a datalog query."
  ([filters]
   (build-clauses filters '?e))
  ([filters entity-sym]
   (->> filters
        (reduce (fn [acc [k v]]
                  (let [prop-sym (keyword->datalog-query-symbol k)]
                    (cond-> acc
                      (vector? v) (update :in conj [prop-sym '...])
                      (not (vector? v)) (update :in conj prop-sym)
                      (map? v) (do
                                 (let [{:keys [in where]} (build-clauses v prop-sym)]
                                   (-> acc
                                       (update :in concat in)
                                       (update :where concat where))))
                      :always (update :where conj
                                      [entity-sym k prop-sym]))))
                {:in    []
                 :where []}))))

(defn build-query
  "Given a map of where clauses (each key is a property and each value is a value to match)
  build a query that can be used with datalog."
  [{:keys [keys where datomic?] :as _query-opts} entity-id-key]
  (let [{where-clauses :where
         :keys         [in]}
        (build-clauses where)
        find-clauses (or (when-let [keys (seq keys)]
                           [(list 'pull '?e (vec keys))])
                         '[(pull ?e [*])])]
    {:query (cond-> `{:find  ~find-clauses
                      :in    ~(if datomic? ['$] [])
                      :where [[~'?e ~entity-id-key ~'_]]}
              (seq in) (update :in into in)
              (seq where-clauses) (update :where into where-clauses))
     :args  (vals where)}))