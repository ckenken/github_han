high sampling rate prediction：

1. data -> 放進 raw2

com.ckenken.io
2. UseOptics.java : 設定半徑跟 minPts -> 輸出 cluster 結果檔案 A
3. Same.java:  運行 updateSameIntoRaw() 從A中把 sameid update 進 raw2 table
4. Same.java: 運行 insertSames() -> 把 Same 的資訊放進 same table
5. Same.java: 運行 clusterSameNoCate() -> 輸出 Hot region cluster 結果檔案 B (可以在 function 中調整半徑跟 minPts )
6. Same.java: 運行 updateSameG_NoCate() -> 從 B 中把 hot region id update 進 same table

com.ckenken.implemant.run
7. MakeSequence30.java: 運行 Main_v2.create_sequence30(“han”) -> 創造 sequence30 table
8. IM_Main.java: 確認所有該弄的地方都有加上 _training 後運行 -> 得到 datapattern_training
9. CurrentPredict.java: 調整參數，配合 IM_Main.java 的設定後運行 -> 得到實驗結果

han:
1. data -> 放進 raw2

com.ckenken.io
2. UseOptics.java : 設定半徑跟 minPts -> 輸出 cluster 結果檔 A
3. Same.java:  運行 updateSameIntoRaw() 從A中把 sameid update 進 raw2 table
4. Same.java: 運行 insertSames() -> 把 Same 的資訊放進 same table
5. ClusterG.java: 把 parseSameG() 和 fillCateG(); 註解掉後 運行ｍain()-> 把印出來的所有資訊手動複製到 檔案 C
6. ClusterG.java: 運行 parseSameG() ->  把 hot region 填入 same table
7. ClusterG.java: 運行 Same.find_G_center() -> 創造 gcenter table
8. ClusterG.java: 運行 fillCateG -> 把 category 資訊放入 gcenter

com.ckenken.implement.run
9. MakeSequence30.java: 運行 Main_v2.create_sequence30(“han”) -> 創造 sequence30 table
10. MakeSequence30.java: 運行 Main_v2.timeDistribution() -> 填好 timedistribuction table
11. MakeSequence30.java: 運行 Main_v2.mergeTimeDistribution() -> 填好 g_timedistribution

com.ckenken.Main
12. 運行 Main_v2.java -> 得到 coarse 以及 fine table 內容（pattern）, 請確認MAX_POINT_NUM 是 same 的種類數，另外可以調整 sigma 以及 DT

com.ckenken.implement.run
13. 運行 predictionByHan.java -> 得到實驗結果  // 記得改 80% 數字 約 52 行

low sampling rate:
1. 讀入 checkin data 放進 raw2, 把 timestamp 跟 day 還有 timenumber 另外補上去

com.ckenken.implement.sparse
2. ParseCheckin.java: 運行 removeNoise() -> 輸出很多檔案進 D 資料夾
3. ParseCheckin.java: 運行 updateSame() -> 從D的檔案們中寫入 sameid 進 raw2
4. ParseCheckin.java: 運行 insertSames() -> 創建 same table
5. ParseCheckin.java: 運行 clusterSameNoCate() 以及 updateG_NoCate() -> 將每個 timestamp 的 gid 寫入 same table
6. ParseCheckin.java: 運行 find_G_center() -> 創建 temp_gcenter 
7. ParseCheckin.java: 運行 insertGcenterMap() -> 把相近的 gcenter 的 map 建入 gcenter_map table
8. Gcenter.java: 解開 main 中到 306 行為止的註解，但記得註解掉 fillgfocusIntoraw() 以及 insertCategories()， 運行 main -> 創建 gcenter table
9. Gcenter.java: 註解掉其他東西運行 fillgfocusIntoraw()  -> 把 gid 填入 raw2
10. Gcenter.java: 註解掉其他東西運行 insertCategories() -> 把 categories 填入 gcenter table
11. BuildTrajectory.java: 運行 createSequence22() -> 創建 sequence22
12. TPMiner.java: 運行 -> 得到 datapattern (參數去 IM_Main.java 改)
13. Ranked_Coverage.java: 運行得到實驗結果 (參數去 IM_Main.java 改)

information loss rate: 
ErrorRate.java -> 運行 main 得到結果 (參數去 IM_Main.java 改)




