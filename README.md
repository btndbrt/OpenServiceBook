# OpenServiceBook

An open service book for keeping track of car maintenance.

## Requirements

- JDK 25
- Linux or macOS for the `osb` command (the launcher is a shell script)

Maven is not required; the bundled Maven Wrapper (`./mvnw`) downloads it on first use.

## Install

Build the jar, then install the `osb` command:

```sh
./mvnw verify
java -jar target/openservicebook-0.1.0-SNAPSHOT.jar install
osb help
```

`install` copies the jar to `~/.openservicebook/osb.jar` and creates an `osb` launcher in `~/.local/bin`.
If your shell says `osb: command not found`, add `~/.local/bin` to your `PATH`.

After changing the code, run the same two commands again to update the installed copy.

## Usage

### Add a car

```sh
osb add-car <vin> <plate> <make> <model> <year>
osb add-car WVWZZZ1JZXW000001 ABC-123 Volkswagen Golf 1999
```

### List your cars

```sh
osb list-cars
```

### Record a service

```sh
osb add-service <vin-or-plate> <date> <mileage> <cost> <description>
osb add-service ABC-123 2026-09-01 120000 89.90 Oil change
```

- Date is written as `YYYY-MM-DD`.
- Mileage is a whole number.
- Cost is a plain amount with a dot: `89.90`, `89.9` or `90`.
- Everything after the cost is the description, so quotes are not needed.

### Show a car's service history

```sh
osb services <vin-or-plate>
osb services ABC-123
```

```
2026-09-01    120000       89.90  Oil change
```

### Show all commands

```sh
osb help
```

## Good to know

- Wherever a command asks for a car, you can use its VIN or its plate. Case, spaces and dashes are ignored,
  so `abc123`, `ABC 123` and `ABC-123` all find the same car.
- Your data is saved to `~/.openservicebook/servicebook.txt`. It is plain text, one car or service per line
  with fields separated by tabs, so it is easy to back up or read. If you edit it by hand, keep the tabs;
  the program reports the line number if something is wrong.
- Cars and services cannot be edited or deleted from the command line yet; to fix a typo, edit the file.
