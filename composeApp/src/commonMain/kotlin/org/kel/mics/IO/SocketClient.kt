package org.kel.mics.IO

import arrow.core.Either
import org.kel.mics.Mal.MalType

suspend expect fun createClientSocket(address: String, port: Int,  message: String) : String

suspend expect fun dispatchSocketCall(address: String, port: Int,  message: String) : Either<MalType, MalType>
