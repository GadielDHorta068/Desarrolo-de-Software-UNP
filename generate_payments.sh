#!/bin/bash

TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJnYWRpZWwwNjhAZ21haWwuY29tIiwiaWF0IjoxNzYxNjgxMjQ1LCJleHAiOjE3NjE3Njc2NDV9.FZccNYpVod23gKUTEI7_PlPo7xgnHBFMxADg6kKJsx8"
URL="http://localhost:8080/payments/create"

# Arrays of existing data
declare -a event_ids=(1 2 3 4 5 6 7 8 9 10 11 12 13 14)
declare -a receiver_ids=(2 3 4 5 6 7 8 9 10 11)

# Function to get a random element from an array
function get_random_element() {
  local arr=("$@")
  local len=${#arr[@]}
  local index=$(($RANDOM % $len))
  echo "${arr[$index]}"
}

for i in {1..30}
do
  EVENT_ID=$(get_random_element "${event_ids[@]}")
  RECEIVER_ID=$(get_random_element "${receiver_ids[@]}")
  AMOUNT=$(shuf -i 100-5000 -n 1)
  
  curl -X POST "$URL?paymentId=$i&externalReference=ref_$i&userId=1&eventId=$EVENT_ID&receiverId=$RECEIVER_ID&amount=$AMOUNT&currency=ARS&paymentMethodId=credit_card&paymentTypeId=one_time" \
    -H "Authorization: Bearer $TOKEN"
done