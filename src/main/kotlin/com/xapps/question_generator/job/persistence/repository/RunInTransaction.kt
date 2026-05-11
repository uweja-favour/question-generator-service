//package com.xapps.question_generator.job.persistence.repositories
//
//import org.springframework.stereotype.Component
//import org.springframework.transaction.reactive.TransactionalOperator
//import org.springframework.transaction.reactive.executeAndAwait
//
//@Component
//class RunInTransaction(
//    private val transactionalOperator: TransactionalOperator
//) {
//    suspend operator fun <T> invoke(block: suspend () -> T): T =
//        transactionalOperator.executeAndAwait {
//            block()
//        }
//}