# AxaSparkChallenge
## How to Run Code:

•	Make sure you have docker installed in machine and path variable is set

•	Clone the code from github https://github.com/abhiroks/AxaSparkChallenge.git and CD to location where code is cloned

•	Execute below command

       docker build -t axa/docker-spark-submit:spark-2.4.1 .

•	Execute Below command. Please make sure to replace parameter SPARK_DRIVER_HOST="local_ip_address" with your machine local IP address and then execute the below command

        docker run -it -p 5000-5010:5000-5010 -e SCM_URL="https://github.com/abhiroks/AxaSparkChallenge.git" -e SPARK_DRIVER_HOST="local_ip_address" -e MAIN_CLASS="com.axa.jobs.job" axa/docker-spark-submit:spark-2.4.1
        
•	you can also login to the docker container and traverse to /app/data/output-sum or /app/data/output-count folder to check the output. You can achieve this by opening a new window and login to running docker container. I have ran an infinite loop to make the docker container remains alive

        docker ps

        docker exec -it <containerId> bash
        
• run.sh script has information related to spark-submit job and it is also responsible for downloading the code from git and creating jar file using SBT inside docker container
  
