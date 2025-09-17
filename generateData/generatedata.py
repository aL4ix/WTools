from collections import namedtuple

from faker import Faker

from ssie2 import ssie


def main():
    res = []
    fake = Faker()
    nt_cols = 'FirstName LastName Email UserName Password OrgSecond'
    ss_cols = ['First Name', 'Last Name', 'Email', 'UserName', 'Password', 'Org/secondary username']
    Row = namedtuple('Row', nt_cols)
    for i in range(10):
        user_name = fake.user_name() + str(fake.random_digit()) + str(fake.random_digit()) + str(fake.random_digit()) \
                    + str(fake.random_digit()) + str(fake.random_digit()) + str(fake.random_digit())
        res.append(Row(fake.first_name() + fake.first_name() + fake.first_name() + fake.first_name() \
                       + fake.first_name() + fake.first_name() + fake.first_name() + fake.first_name() \
                       + fake.first_name() + fake.first_name() + fake.first_name() + fake.first_name() \
                       + fake.first_name() + fake.first_name() + fake.first_name() + fake.first_name(),
                       fake.last_name() + fake.last_name() + fake.last_name() + fake.last_name() \
                       + fake.last_name() + fake.last_name() + fake.last_name() + fake.last_name() \
                       + fake.last_name() + fake.last_name() + fake.last_name() + fake.last_name() \
                       + fake.last_name() + fake.last_name() + fake.last_name() + fake.last_name()
                       , fake.email(), 'CLH_' + user_name,
                       fake.password(20, True, True, True, True),
                       user_name))
    ss = ssie.SpreadSheet(res, columns=ss_cols)
    ss.to_file('generatedData.xls')
    print("Generated data successfully.")


if __name__ == '__main__':
    main()
