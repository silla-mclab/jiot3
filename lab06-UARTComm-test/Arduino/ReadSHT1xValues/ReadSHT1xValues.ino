#include <SHT1x.h>

//#define TESTING

// Specify data and clock connections and instantiate SHT1x object
#define dataPin  2
#define clockPin 3
SHT1x sht1x(dataPin, clockPin);

#ifdef  TESTING
#define COMM_DEV    Serial
#else
#define COMM_DEV    Serial1
#endif

char* reqCmds[3] = {
  "TMP", "HMD", "ACK"
};

char cmdBuf[32];

#define BUF_LENGTH  10
float tmpValues[BUF_LENGTH], tmpSum = 0, tmpAvg;
float hmdValues[BUF_LENGTH], hmdSum = 0, hmdAvg;

void setup()
{
   COMM_DEV.begin(115200);
#ifdef  TESTING   
   COMM_DEV.println("Starting SHT11 monitoring...");
   COMM_DEV.println("Initialize tmp/hmd value buffer...");
#endif
   initSavedValues();
#ifdef  TESTING   
   COMM_DEV.print("Avg. temperature = "); COMM_DEV.println(tmpAvg);
   COMM_DEV.print("Avg. humidity = "); COMM_DEV.println(hmdAvg);
#endif   
}

void initSavedValues() {
  float val = 0;
  for (int i=0; i<BUF_LENGTH; i++) {
    val = sht1x.readTemperatureC();
    tmpValues[i] = val;
    tmpSum += val;

    val = sht1x.readHumidity();
    hmdValues[i] = val;
    hmdSum += val;
  }
  tmpAvg = tmpSum / BUF_LENGTH;
  hmdAvg = hmdSum / BUF_LENGTH;
}

void updateTemperature() {
  static int idx = 0;

  float val = sht1x.readTemperatureC();
  tmpSum -= tmpValues[idx];
  tmpValues[idx] = val;
  tmpSum += val;
  tmpAvg = tmpSum / BUF_LENGTH;
  idx = (idx + 1) % BUF_LENGTH;
}

void updateHumidity() {
  static int idx = 0;

  float val = sht1x.readHumidity();
  hmdSum -= hmdValues[idx];
  hmdValues[idx] = val;
  hmdSum += val;
  hmdAvg = hmdSum / BUF_LENGTH;
  idx = (idx + 1) % BUF_LENGTH;
}

boolean getReqCmd(char* cmd) {
  boolean result = false;
  int ch, count = 0;
  
  if (COMM_DEV.available() >= 4) {
    do {
      ch = COMM_DEV.read();
    } while (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n');
    while (ch != -1 && ch != '\n') {
      *cmd++ = ch;
      count++;
      ch = COMM_DEV.read();
    }
    if (count >= 3)  result = true;
  } 
  *cmd = '\0';
    
  return result;
}

void processReqCmd(char *cmd) {
  int idx = 0;

  for (idx=0; idx<3; idx++) {
    if (strncmp(reqCmds[idx], cmd, 3) == 0)
      break;
  }

  switch (idx) {
  case 0:
    COMM_DEV.print("T="); COMM_DEV.print(tmpAvg); COMM_DEV.write('\n');
    break;
  case 1:
    COMM_DEV.print("H="); COMM_DEV.print(hmdAvg); COMM_DEV.write('\n');
    break;
  case 2:
    COMM_DEV.print("OK"); COMM_DEV.write('\n');
    break;
  }
}

void loop()
{
  if (getReqCmd(cmdBuf)) {
    processReqCmd(cmdBuf);
  }
  updateTemperature();
  updateHumidity();
  delay(100);
}
