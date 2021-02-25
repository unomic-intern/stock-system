resolvers in ThisBuild += "lightbend-commercial-mvn" at
  "https://repo.lightbend.com/pass/umnbIaQfPcWnAVwXXDSDu06ztdvV_aRPpjee7rdni_-mtiio/commercial-releases"
resolvers in ThisBuild += Resolver.url("lightbend-commercial-ivy",
  url("https://repo.lightbend.com/pass/umnbIaQfPcWnAVwXXDSDu06ztdvV_aRPpjee7rdni_-mtiio/commercial-releases"))(Resolver.ivyStylePatterns)