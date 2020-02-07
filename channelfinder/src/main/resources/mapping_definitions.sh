#!/usr/bin/env bash

###
# #%L
# ChannelFinder Directory Service
# %%
# Copyright (C) 2020        Lawrence Berkeley National Laboratory
# %%
# Copyright (C) 2010 - 2016 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
# %%
# Copyright (C) 2010 - 2012 Brookhaven National Laboratory
# All rights reserved. Use is subject to license terms.
# #L%
###

# The mapping definition for the Indexes associated with the channelfinder v2

function print_help()
{
    local type=$1; shift;
    local index=$1; shift;
    printf "\n>>> %s : %s ....\n" "$type" "$index";
}

#Create the Index
print_help "index" "tags"
curl -XPUT 'http://localhost:9200/tags'

#Set the mapping
print_help "mapping" "tags"
curl -H 'Content-Type: application/json' -XPUT 'http://localhost:9200/tags/_mapping/tag' -d'
{
  "tag" : {
    "properties" : {
      "name" : {
        "type" : "text"
      },
      "owner" : {
        "type" : "text"
      }
    }
  }
}'

print_help "index" "properties"
curl -XPUT 'http://localhost:9200/properties'
print_help "mapping" "properties"
curl -H 'Content-Type: application/json' -XPUT 'http://localhost:9200/properties/_mapping/property' -d'
{
  "property" : {
    "properties" : {
      "name" : {
        "type" : "text"
      },
      "owner" : {
        "type" : "text"
      }
    }
  }
}'

print_help "index" "channelfinder"
curl -XPUT 'http://localhost:9200/channelfinder'
print_help "mapping" "channelfinder"
curl -H 'Content-Type: application/json' -XPUT 'http://localhost:9200/channelfinder/_mapping/channel' -d'
{
  "channel" : {
    "properties" : {
      "name" : {
        "type" : "text",
        "analyzer" : "whitespace"
      },
      "owner" : {
        "type" : "text",
        "analyzer" : "whitespace"
      },
      "script" : {
        "type" : "text"
      },
      "properties" : {
        "type" : "nested",
        "include_in_parent" : true,
        "properties" : {
          "name" : {
            "type" : "text",
            "analyzer" : "whitespace"
          },
          "owner" : {
            "type" : "text"
          },
          "value" : {
            "type" : "text",
            "analyzer" : "whitespace"
          }
        }
      },
      "tags" : {
        "type" : "nested",
        "include_in_parent" : true,
        "properties" : {
          "name" : {
            "type" : "text",
            "analyzer" : "whitespace"
          },
          "owner" : {
            "type" : "text",
            "analyzer" : "whitespace"
          }
        }
      }
    }
  }
}'

printf "\n";
