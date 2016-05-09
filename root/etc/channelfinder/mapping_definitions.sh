#!/bin/bash

###
# #%L
# ChannelFinder Directory Service
# %%
# Copyright (C) 2010 - 2016 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
# %%
# Copyright (C) 2010 - 2012 Brookhaven National Laboratory
# All rights reserved. Use is subject to license terms.
# #L%
###

# The mapping definition for the Indexes associated with the channelfinder v2


#Create the Index
curl -XPUT '${ELASTIC_HOST}/tags'
#Set the mapping
curl -XPUT '${ELASTIC_HOST}/tags/_mapping/tag' -d'
{
  "tag" : {
    "properties" : {
      "name" : {
        "type" : "string"
      },
      "owner" : {
        "type" : "string"
      }
    }
  }
}'

curl -XPUT '${ELASTIC_HOST}/properties'
curl -XPUT '${ELASTIC_HOST}/properties/_mapping/property' -d'
{
  "property" : {
    "properties" : {
      "name" : {
        "type" : "string"
      },
      "owner" : {
        "type" : "string"
      }
    }
  }
}'

curl -XPUT '${ELASTIC_HOST}/channelfinder'
curl -XPUT '${ELASTIC_HOST}/channelfinder/_mapping/channel' -d'
{
  "channel" : {
    "properties" : {
      "name" : {
        "type" : "string",
        "analyzer" : "whitespace"
      },
      "owner" : {
        "type" : "string",
        "analyzer" : "whitespace"
      },
      "script" : {
        "type" : "string"
      },
      "properties" : {
        "type" : "nested",
        "include_in_parent" : true,
        "properties" : {
          "name" : {
            "type" : "string",
            "analyzer" : "whitespace"
          },
          "owner" : {
            "type" : "string"
          },
          "value" : {
            "type" : "string",
            "analyzer" : "whitespace"
          }
        }
      },
      "tags" : {
        "type" : "nested",
        "include_in_parent" : true,
        "properties" : {
          "name" : {
            "type" : "string",
            "analyzer" : "whitespace"
          },
          "owner" : {
            "type" : "string",
            "analyzer" : "whitespace"
          }
        }
      }
    }
  }
}'
