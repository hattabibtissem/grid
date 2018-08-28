package lib.elasticsearch

import com.gu.mediaservice.lib.elasticsearch.ImageFields
import com.gu.mediaservice.model.usage.SyndicationUsage
import com.gu.mediaservice.model._
import org.elasticsearch.index.query.FilterBuilder
import org.joda.time.DateTime

object SyndicationFilter extends ImageFields {
  private def syndicationRightsAcquired(acquired: Boolean): FilterBuilder = filters.boolTerm(
    field = "syndicationRights.rights.acquired",
    value = acquired
  )

  private val noRightsAcquired: FilterBuilder = filters.or(
    filters.existsOrMissing("syndicationRights.rights.acquired", exists = false),
    syndicationRightsAcquired(false)
  )

  private val hasRightsAcquired: FilterBuilder = filters.bool.must(
    syndicationRightsAcquired(true)
  )

  private val hasAllowLease: FilterBuilder = filters.term(
    "leases.leases.access",
    AllowSyndicationLease.name
  )

  private val hasDenyLease: FilterBuilder = filters.term(
    "leases.leases.access",
    DenySyndicationLease.name
  )

  private val hasSyndicationUsage: FilterBuilder = filters.term(
    "usages.platform",
    SyndicationUsage.toString
  )

  private val leaseHasStarted: FilterBuilder = filters.or(
    filters.existsOrMissing("leases.leases.startDate", exists = false),
    filters.date("leases.leases.startDate", None, Some(DateTime.now)).get
  )

  private val leaseHasEnded: FilterBuilder = filters.or(
    filters.existsOrMissing("leases.leases.endDate", exists = false),
    filters.date("leases.leases.endDate", Some(DateTime.now), None).get
  )

  private val syndicationRightsPublished: FilterBuilder = filters.or(
    filters.existsOrMissing("syndicationRights.published", exists = false),
    filters.date("syndicationRights.published", None, Some(DateTime.now)).get
  )

  def statusFilter(status: SyndicationStatus): FilterBuilder = status match {
    case SentForSyndication => filters.and(
      hasRightsAcquired,
      hasAllowLease,
      hasSyndicationUsage
    )
    case QueuedForSyndication => filters.and(
      hasRightsAcquired,
      filters.bool.mustNot(hasSyndicationUsage),
      filters.and(
        hasAllowLease,
        leaseHasStarted,
        syndicationRightsPublished
      )
    )
    case BlockedForSyndication => filters.and(
      hasRightsAcquired,
      hasDenyLease
    )
    case AwaitingReviewForSyndication => filters.and(
      hasRightsAcquired,
      filters.bool.mustNot(
        hasAllowLease,
        filters.and(
          hasDenyLease,
          leaseHasEnded
        )
      )
    )
    case UnsuitableForSyndication => noRightsAcquired
  }
}
