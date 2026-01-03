# PT Chat Bot 🏃‍♂️🩺

A Java-based Physical Therapy training plan generator that uses pain, recovery, and readiness signals to recommend safe daily training decisions.

This project simulates how a PT or coach might adjust training based on injury status, pain levels, sleep, and recovery indicators.

---

## 🚀 Features

- Injury-aware training logic
- RED / YELLOW / GREEN readiness system
- Rehab plan suggestions (hamstring, knee, calf)
- Input validation for safer recommendations
- Unit testing with JUnit 5
- Continuous Integration using GitHub Actions

---

## 🧠 How It Works

The program prompts the user for:
- Pain at rest and during activity
- Morning stiffness
- Sleep duration
- Injury red flags (swelling, sharp pain, instability)
- General training readiness (strength, flexibility, CNS)

Based on these inputs, the system assigns a readiness zone:

- **GREEN** → Full training allowed
- **YELLOW** → Modified or reduced-intensity training
- **RED** → Recovery-focused day (no sprinting)

Each zone outputs a recommended daily training plan.

---

## ▶️ Running the Program

Compile and run the program from the command line:

'''bash
javac TrainingPlanGenerator.java
java TrainingPlanGenerator

## 🔮 Future Improvements

Planned and potential enhancements for this project include:

- Interactive chat-style commands (`checkin`, `plan`, `rehab`)
- Sprint-specific training logic:
  - Acceleration vs max-velocity day selection
  - Deceleration and volume limits
- Training history logging and trend analysis
- Readiness scoring over time
- Expanded injury modules (IT band, hip flexor, Achilles)
- Web-based interface (Spring Boot / REST API)
- Mobile-friendly UI
- Data visualization for recovery and performance trends

## 👤 Author

Built by Caiden Smith

Computer Engineering student with interests in:
- Software engineering
- Systems & IT
- Sports performance and rehabilitation technology

This project was built end-to-end using Java, automated testing, and CI/CD to simulate real-world decision logic in a training and rehab context.
