# CB-ILOF

CB-ILOF is a cube-based incremental outlier detection algorithm for streaming computing. In our algorithm, the space of streaming data is divided into multiple cubes, and each cube is assigned an outlier factor. By this means the update of intermediate parameters of outlier factors of affected data points is transferred to the update of affected cubes, which significantly reduces the overhead of execution time and runtime memory. 
