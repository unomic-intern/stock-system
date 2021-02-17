resolvers in ThisBuild += "lightbend-commercial-mvn" at
  "https://repo.lightbend.com/pass/cGLUWqkE_FhPt9pJl7vHgvSZxEbjnxy7K5r-wUAQ6YhPMMHW/commercial-releases"
resolvers in ThisBuild += Resolver.url("lightbend-commercial-ivy",
  url("https://repo.lightbend.com/pass/cGLUWqkE_FhPt9pJl7vHgvSZxEbjnxy7K5r-wUAQ6YhPMMHW/commercial-releases"))(Resolver.ivyStylePatterns)
