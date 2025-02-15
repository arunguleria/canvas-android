// Copyright (C) 2023 - present Instructure, Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 3 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter_parent/models/terms_of_service.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/network/api/user_api.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class AcceptableUsePolicyInteractor {
  Future<TermsOfService> getTermsOfService() {
    return locator<AccountsApi>().getTermsOfService();
  }

  Future<User> acceptTermsOfUse() {
    return locator<UserApi>().acceptUserTermsOfUse();
  }
}