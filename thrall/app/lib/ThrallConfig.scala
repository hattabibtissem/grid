package lib

import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.gu.mediaservice.lib.config.CommonConfig
import com.gu.mediaservice.lib.elasticsearch.EC2
import play.api.Configuration

class ThrallConfig(override val configuration: Configuration) extends CommonConfig {
  private lazy val ec2Client = withAWSCredentials(AmazonEC2ClientBuilder.standard()).build()

  final override lazy val appName = "thrall"

  lazy val queueUrl: String = properties("sqs.queue.url")

  lazy val imageBucket: String = properties("s3.image.bucket")

  lazy val writeAlias = properties.getOrElse("es.index.aliases.write", configuration.get[String]("es.index.aliases.write"))

  lazy val thumbnailBucket: String = properties("s3.thumb.bucket")

  lazy val elasticsearchHost: String =
    if (isDev)
      properties.getOrElse("es.host", "localhost")
    else
      EC2.findElasticsearchHostByTags(ec2Client, Map(
        "Stage" -> Seq(stage),
        "Stack" -> Seq(elasticsearchStack),
        "App" -> Seq(elasticsearchApp)
      ))

  lazy val elasticsearchPort: Int = properties("es.port").toInt
  lazy val elasticsearchCluster: String = properties("es.cluster")

  lazy val elasticsearch6Host: String =
    if (isDev)
      properties.getOrElse("es6.host", "localhost")
    else
      EC2.findElasticsearchHostByTags(ec2Client, Map(
        "Stage" -> Seq(stage),
        "Stack" -> Seq(elasticsearchStack),
        "App" -> Seq(elasticsearch6App)
      ))

  lazy val elasticsearch6Port: Int = properties("es6.port").toInt
  lazy val elasticsearch6Cluster: String = properties("es6.cluster")
  lazy val elasticsearch6Shards: Int = if (isDev) 1 else properties("es6.shards").toInt
  lazy val elasticsearch6Replicas: Int = if (isDev) 0 else properties("es6.replicas").toInt

  lazy val healthyMessageRate: Int = properties("sqs.message.min.frequency").toInt

  lazy val dynamoTopicArn: String = properties("indexed.image.sns.topic.arn")

  lazy val topicArn: String = properties("sns.topic.arn")
}
