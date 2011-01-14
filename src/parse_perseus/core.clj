(ns parse_perseus.core)

;; Beta Code = bc = funny combinations of chars

(def iliad_first_line_bc "mh=nin a)/eide qea\\ *phlhi+a/dew *)axilh=os")
(def odyssey_first_line_bc "a)/ndra moi e)/nnepe, mou=sa, polu/tropon, o(\\s ma/la polla\\")
(def odyssey_first_line_gk "ἄνδρα μοι ἔννεπε, μοῦσα, πολύτροπον, ὃς μάλα πολλὰ")

;; Texts in ~/source/perseus/texts/ ...

;;; TODO ;;;
;; Put the greek above in an epub book...
;; Convert to mobi
;; Read on kindle
;; Does it work?

(defn bc-to-gk [bc-text]
  bc-text)