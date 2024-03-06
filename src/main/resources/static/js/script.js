const toggleSidebar = () => {
  if ($(".sidebar").is(":visible")) {
    $(".sidebar").css("display", "none");
    $(".content").css("margin-left", "0%");
  } else {
    $(".sidebar").css("display", "block");

    $(".content").css("margin-left", "20%");
  }
};
const search = () => {
  //console.log("search");
  let query = $("#search-input").val();

  if (query == "") {
    $(".search-result").hide();
  } else {
    //  console.log(query);

    let url = `http://localhost:8080/search/${query}`;
    fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((data) => {
        //  console.log(data);
        let text = `<div class='list-group'>`;
        data.forEach((contact) => {
          text += `<a href='/user/${contact.cid}/contact' class='list-group-item list-group-item-action'>${contact.name} </a>`;
        });
        text += `</div>`;

        $(".search-result").html(text);
        $(".search-result").show();
      });
    $(".search-result").show();
    $(".search-result").show();
  }
};

const paymentStrat = () => {
  console.log("paymentstart");
  let amount = $("#paymentField").prev().val(); // Use .val() to get the value of the input field
  console.log(amount);
  if (amount == "" || amount == null) {
    Swal.fire({
      title: "Failed!!",
      text: "Amount is required",
      icon: "error",
    });
    return;
  }

  $.ajax({
    url: "/user/create_order",
    data: JSON.stringify({ amount: amount, info: "order_request" }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (response) {
      console.log(response);
      if (response.status == "created") {
        let options = {
          key: "rzp_test_3Q0vQxOHghJlJC",
          amount: response.amount,
          currency: "INR",
          name: "Smarr Contact Manager",
          description: "Testing ",
          image:
            "https://i.pinimg.com/564x/35/f3/76/35f376e195ea8e94c18cc7da71cc48ce.jpg",
          order_id: response.id,
          handler: function (response) {
            console.log(response.razorpay_payment_id);
            console.log(response.razorpay_order_id);
            console.log(response.razorpay_signature);
            console.log("payment successfull!!");
            
            updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,"paid");
            // alert("congrates!! payment successfull!!");
           
          },
          prefill: {
            name: "",
            email: "",
            contact: "",
          },
          notes: {
            address: "Learning payment integration",
          },
          theme: {
            color: "#3399cc",
          },
        };
        let rzp = new Razorpay(options);
        rzp.on("payment.failed", function (response) {
          console.log(response.error.description);
          console.log(response.error.code);
          console.log(response.error.source);
          console.log(response.error.step);
          console.log(response.error.reason);
          console.log(response.error.metadata.order_id);
          console.log(response.error.metadata.payment_id);

          Swal.fire({
            title: "Failed!!",
            text: "Oops payment failed",
            icon: "error",
          });
        });
        rzp.open();
      }
    },
    error: function (error) {
      console.log(error);
      alert("something wen wrong!!");
    },
  });
};


function updatePaymentOnServer(payment_id, order_id, statuss) {
	$.ajax({
    url: "/user/update_order",
    data: JSON.stringify({ payment_id: payment_id, order_id: order_id,statuss:statuss }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success:function(response){
		 Swal.fire({
              title: "Good job!",
              text: "Congrats!! Payment successful!!",
              icon: "success",
            });
	},
	error:function(error){
		Swal.fire({
				title: "Failed!!",
				text: "Your payment is successfull, but we did not get on server, we will contact you as soon as possible",
				icon: "error",
			});
	}
    })
}