library(BSDA)    # For Z test

set.seed(8)
p <- data.frame(val=rnorm(1000000, 10, 1))
noquote(paste0("Population mean: ", mean(p$val)))
noquote(paste0("Population std:  ", mean(p$val)))
N <- 100    # Sample size
X <- sample(p$val, N, replace=F)
z.test(X, sigma.x=1, mu=10, conf.level=0.90)
z.test(X, sigma.x=1, mu=10, conf.level=0.95)
