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
  1  2026-09-01    120000       89.90  Oil change
  2  2026-09-15    120400      250.00  Brake pads
```

The number at the start of each line is how `edit-service` and `delete-service` pick a service.

### Edit a car or a service

Change one field at a time:

```sh
osb edit-car <vin-or-plate> <field> <new-value>
osb edit-car ABC-123 plate XYZ-999
osb edit-car ABC-123 model Golf GTI

osb edit-service <vin-or-plate> <number> <field> <new-value>
osb edit-service ABC-123 2 cost 95.00
osb edit-service ABC-123 2 description Front brake pads
```

- Car fields: `vin`, `plate`, `make`, `model`, `year`.
- Service fields: `date`, `mileage`, `cost`, `description`.
- New values follow the same rules as when adding, and may contain spaces without quotes.

### Delete a car or a service

```sh
osb delete-service <vin-or-plate> <number>
osb delete-service ABC-123 2

osb delete-car <vin-or-plate>
osb delete-car ABC-123
```

Deleting a car also deletes all of its services. There is no undo, so back up
`~/.openservicebook/servicebook.txt` first if you are unsure.

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
