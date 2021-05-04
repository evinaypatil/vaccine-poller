# Vaccine Poller

Commands to run the program
```
cd vaccine-poller
mvn clean package
java -jar ./target/vaccine-poller-1.0.0-SNAPSHOT-jar-with-dependencies.jar 05-05-2021,294,18 24-05-2021,294,45,paid
```

Arguments:
```
Provide 1 or more arguments. Each argument is a comma separated string containing
1. Date (mm-dd-yyyy) (required)
2. District ID (Integer obtained from cowin website. Do a inspect, and identify your District ID) (required)
3. Age limit - 18/45 (required)
4. Fee type - free/paid (optional)

Examples:
a) 05-05-2021,294,18      - Fetches 7 days data starting from 5-11th may 2021 (7 days), check if there is FREE/PAID vaccine availability in district 294 for 18 age limit
b) 24-05-2021,294,45,paid - Fetches 7 days data starting from 24-30th may 2021 (7 days), check if there is PAID vaccine availability in district 294 for 45 age limit

If there is vaccine availability, the program prints the vaccination center(s) details, and terminates.
```

Note: There might be bugs in the poller. So please use the utility and rely on it at your own risk.

Output:
![image](https://user-images.githubusercontent.com/64588033/116989654-a4fae780-acef-11eb-9437-f11991c80360.png)
