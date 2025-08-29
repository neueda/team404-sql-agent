function give_feedback() {
  //print the user's query back to the page for verification
  const userInput = document.getElementById("user_input").value;
  //console.log(userInput)
  const feedback_element = document.getElementById("feedback_element");
  if (userInput) {
    send_request(userInput)
    feedback_element.innerText = userInput;
  }
  else {
    feedback_element.innerText = "No query entered"
  }
}

async function send_request(user_input) {
  const url = `http://localhost:8080/api/hello?query=${user_input}`; //change to actual URL when we have GC access
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Response status: ${response.status}`);
    }

    const results = await response.json();

    use_response(results);

  } catch (error) {
    console.error(error.message);
  }
}

function use_response(result_json) {
  //Populates the HTML Table element with JSON object.
  const table = document.getElementById("results");

  // Loop through all keys in the result_json object

  for (let key in result_json) {
    if (result_json.hasOwnProperty(key)) {
      table.innerHTML += `${key} : ${result_json[key]}<br>`;
    }
  }
}