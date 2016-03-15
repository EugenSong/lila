package lila.push

import org.joda.time.DateTime
import reactivemongo.bson._

import lila.db.BSON._
import lila.db.Types.Coll
import lila.user.User

private final class DeviceApi(coll: Coll) {

  private implicit val DeviceBSONHandler = Macros.handler[Device]

  private[push] def findByDeviceId(deviceId: String): Fu[Option[Device]] =
    coll.find(BSONDocument("_id" -> deviceId)).one[Device]

  private[push] def findByUserId(userId: String): Fu[List[Device]] =
    coll.find(BSONDocument("userId" -> userId)).cursor[Device]().collect[List]()

  private[push] def findLastByUserId(userId: String): Fu[Option[Device]] =
    coll.find(BSONDocument("userId" -> userId))
      .sort(BSONDocument("seenAt" -> -1))
      .one[Device]

  def register(user: User, platform: String, deviceId: String) = {
    lila.mon.push.register.in(platform)()
    coll.update(BSONDocument("_id" -> transformId(deviceId, platform)), Device(
      _id = deviceId,
      platform = platform,
      userId = user.id,
      seenAt = DateTime.now
    ), upsert = true).void
  }

  private def transformId(id: String, platform: String) = platform match {
    case "ios" => id.grouped(8).mkString("<", " ", ">")
    case _     => id
  }

  def unregister(user: User) = {
    lila.mon.push.register.out()
    coll.remove(BSONDocument("userId" -> user.id)).void
  }
}
