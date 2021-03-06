/*
 * Copyright 2020 Qiniu Cloud (qiniu.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qiniu.stream.spark.config

import com.qiniu.stream.spark.core.JobContext
import com.qiniu.stream.spark.statement._
import org.apache.spark.sql.SparkSession

object RichStatement {

  implicit class RichStatement(statement: Statement) {
    def execute(jobContext: JobContext, sparkSession: SparkSession): Unit = {
      val executor = statement match {
        case sourceTable: SourceTable=>
          Some(LoadTableExecutor(sourceTable))
        case sqlQuery: CreateViewStatement =>
          Some(CreateViewExecutor(sqlQuery))
        case insertTable: InsertStatement =>
          Some(WriteTableExecutor(insertTable))
        case createFunctionStatement: CreateFunctionStatement =>
          Some(CreateFunctionExecutor(createFunctionStatement))
        case sqlStatement: SqlStatement =>
          Some(SparkSqlExecutor(sqlStatement))
        case _=> None
      }
      if (!jobContext.isDebugMode) {
        executor.foreach(_.execute(jobContext,sparkSession))
      }
      jobContext.setDebug(statement.inDebugMode())
    }
  }

}

