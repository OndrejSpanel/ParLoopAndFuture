package com.github.ondrejspanel.parFuture

import scala.concurrent._

object CustomEC {
  final lazy implicit val custom: ExecutionContextExecutor = ExecutionContext.fromExecutor(null)

}
